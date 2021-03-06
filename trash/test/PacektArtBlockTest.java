package jp.ac.kansai_u.kutc.firefly.packetArt.test;

import static org.junit.Assert.*;
import jp.ac.kansai_u.kutc.firefly.packetArt.Location;
import jp.ac.kansai_u.kutc.firefly.packetArt.playing.BlockType;
import jp.ac.kansai_u.kutc.firefly.packetArt.playing.PacketBlock;
import jp.ac.kansai_u.kutc.firefly.packetArt.playing.PentoMino;
import jp.ac.kansai_u.kutc.firefly.packetArt.playing.TetroMino;
import jp.ac.kansai_u.kutc.firefly.packetArt.readTcpDump.PcapManager;

import org.jnetpcap.packet.PcapPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PacektArtBlockTest {
	PacketBlock block;
	PcapManager pcapMngr;

	@Before
	public void setUp() throws Exception {
		block = new PacketBlock(new Location(14, 29), PentoMino.T);
		pcapMngr = PcapManager.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		pcapMngr.close();
	}

	@Test
	public void testGetBlockType() {
		assertEquals(BlockType.Mino, block.getBlockType());
	}

	@Test
	public void testSetBlockType() {
		block.setBlockType(BlockType.Wall);
		assertEquals(BlockType.Wall, block.getBlockType());
	}

	@Test
	public void testGetMino() {
		assertEquals(block.getMino(), PentoMino.T);
		assertEquals(block.getBlockType(), BlockType.Mino);
	}

	@Test
	public void testSetMino() {
		block.setMino(TetroMino.LReverse);
		assertEquals(block.getBlockType(), BlockType.Mino);
		assertEquals(block.getMino(), TetroMino.LReverse);
	}

	@Test
	public void testPacket() {
		assertNull(block.getPacket());

		PcapPacket pkt = pcapMngr.nextPacket();
		System.out.println(pkt);
		
		block.setPacket(new PcapPacket(pkt));
		assertNotNull(block.getPacket());
	}

	@Test
	public void testGetLocation() {
		assertEquals(new Location(14, 29) ,block.getLocation());
	}

	@Test
	public void testSetLocationLocation() {
		block.setLocation(new Location());
		assertEquals(new Location(), block.getLocation());
	}

	@Test
	public void testSetLocationIntInt() {
		block.setLocation(5, 5);
		assertEquals(new Location(5, 5), block.getLocation());
	}
}
